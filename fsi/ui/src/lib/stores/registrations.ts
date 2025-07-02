import { writable } from 'svelte/store'
import type { Writable } from 'svelte/store'
import { nanoid } from "nanoid"

interface RegistrationData {
	id: string | null
	email: string | null
	token: string | null
	userId?: string
}

interface RegistrationState {
	data: RegistrationData
	isComplete: boolean
	error: string | null
	loading: boolean
}

interface RegistrationStore extends Writable<RegistrationState> {
	register: (params: { email: string }) => Promise<any>
	authorize: (params: { id: string; code: string }) => Promise<any>
	load: (id: string) => Promise<any>
	saveStep: (stepIndex: number, stepData: any) => Promise<any>
	complete: () => Promise<any>
	reset: () => void
}

// Initialize the store with default values
const createRegistrationsStore = (): RegistrationStore => {
	const initialState: RegistrationState = {
		data: {
			id: null,
			email: null,
			token: null,
		},
		isComplete: false,
		error: null,
		loading: false
	}

	const { subscribe, set, update } = writable(initialState)

	// Helper function to make API requests
	async function apiRequest(url: string, method: string, data: any = null): Promise<Response> {
		try {
			url = 'http://localhost:3030' + url
			const options: RequestInit = {
				method,
				headers: {
					'Content-Type': 'application/json'
				}
			}

			if (data) {
				options.body = JSON.stringify(data)
			}

			const response = await fetch(url, options)

			if (!response.ok) {
				throw new Error(`API error: ${response.status}`)
			}
			return response

		} catch (err) {
			console.error('API request failed:', err)
			throw err
		}
	}

	return {
		subscribe,

		// Initialize a new account process
		register: async ({ email }: { email: string }) => {
			update(state => ({ ...state, loading: true, error: null }))
			const id = nanoid()
			try {
				const response = await apiRequest(`/api/v1/registrations/${id}`, 'PUT', {
					email
				})

				if (!response.ok) {
					throw new Error(`API error: ${response.status}`)
				}

				const d = await apiRequest(`/api/v1/registrations/${id}`, 'GET')
				if (!d.ok) {
					throw new Error(`API error: ${response.status}`)
				}
				const newData = await d.json()
				set({
					...initialState,
					data: { id, email: newData.email, userId: newData.userId, token: newData.token },
					loading: false
				})
				return newData
			} catch (err: any) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to register'
				}))
				throw err
			}
		},

		authorize: async ({ id, code }: { id: string; code: string }) => {
			update(state => ({ ...state, loading: true, error: null }))
			try {
				const response = await apiRequest(`/api/v1/registrations/${id}/authorizations/${code}`, 'PUT')
				if (!response.ok) {
					throw new Error(`API error: ${response.status}`)
				}

				const d = await apiRequest(`/api/v1/registrations/${id}`, 'GET')
				if (!d.ok) {
					throw new Error(`API error: ${response.status}`)
				}
				const newData = await d.json()
				set({
					...initialState,
					data: { id, email: newData.email, userId: newData.userId, token: newData.token },
					loading: false
				})

				return newData
			} catch (err: any) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to register'
				}))
				throw err
			}
		},

		// Load an existing account process
		load: async (id: string) => {
			update(state => ({ ...state, loading: true, error: null }))

			try {
				const response = await apiRequest(`/api/v1/registrations/${id}`, 'GET')
				const data = await response.json()
				set({
					...initialState,
					data: { ...data, token: data.token },
					isComplete: response.isComplete || false,
					error: null,
					loading: false
				})

				return response
			} catch (err: any) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to load account data'
				}))
				throw err
			}
		},

		// Save data for the current step
		saveStep: async (stepIndex: number, stepData: any) => {
			update(state => {
				let newData = { ...state.data }

				// Map step index to data sections
				switch (stepIndex) {
					case 1:
						(newData as any).personalInfo = { ...(newData as any).personalInfo, ...stepData }
						break
					case 2:
						(newData as any).preferences = { ...(newData as any).preferences, ...stepData }
						break
				}

				return {
					...state,
					data: newData,
					loading: true,
					error: null
				}
			})

			// Get current state for API request
			let currentState: RegistrationState
			update(state => {
				currentState = state
				return state
			})

			try {
				// Prepare the patch data for the API
				const patchData = {
					currentStep: stepIndex + 1, // Moving to the next step
					completedSteps: [...new Set([...(currentState! as any).completedSteps || [], stepIndex])],
					data: currentState!.data
				}

				// Make the PATCH request to update the account process
				const response = await apiRequest(
					`/api/v1/onboardings/${currentState!.data.id}`,
					'PATCH',
					patchData
				)

				// Update the store with the response
				update(state => ({
					...state,
					loading: false
				}))

				return response
			} catch (err: any) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to save step data'
				}))
				throw err
			}
		},

		// Complete the account process
		complete: async () => {
			update(state => ({ ...state, loading: true, error: null }))

			// Get current state for API request
			let currentState: RegistrationState
			update(state => {
				currentState = state
				return state
			})

			try {
				// Prepare the patch data for completion
				const patchData = {
					isComplete: true,
					completedSteps: [...new Set([...(currentState! as any).completedSteps || [], 3])], // Add final step
					data: currentState!.data
				}

				// Make the PATCH request to complete the account
				const response = await apiRequest(
					`/api/v1/onboardings/${currentState!.data.id}`,
					'PATCH',
					patchData
				)

				// Update the store with the response
				update(state => ({
					...state,
					isComplete: true,
					loading: false
				}))

				return response
			} catch (err: any) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to complete account'
				}))
				throw err
			}
		},

		// Reset the store to initial state
		reset: () => set(initialState)
	}
}

// Create and export the store
export const registrations = createRegistrationsStore()