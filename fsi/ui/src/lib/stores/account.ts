import { writable } from 'svelte/store'
import type { Writable } from 'svelte/store'

interface AccountData {
	name: string
	clientId: string
	userId: string
	preferences: {
		theme: string
		enableNotifications: boolean
	}
}

interface AccountState {
	id: string | null
	currentStep: number
	completedSteps: number[]
	data: AccountData
	isComplete: boolean
	error: string | null
	loading: boolean
}

interface AccountStore extends Writable<AccountState> {
	start: (params: { id: string }) => Promise<string>
	matchClient: (params: { id: string; ssn: string; birthdate: string; name: string }) => Promise<any>
	load: (id: string) => Promise<any>
	saveStep: (stepIndex: number, stepData: any) => Promise<any>
	complete: () => Promise<any>
	reset: () => void
}

// Initialize the store with default values
const createAccountStore = (): AccountStore => {
	const initialState: AccountState = {
		id: null,
		currentStep: 0,
		completedSteps: [],
		data: {
			name: '',
			clientId: '',
			userId: '',
			preferences: {
				theme: 'light',
				enableNotifications: false
			},
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
		start: async ({ id }: { id: string }) => {
			update(state => ({ ...state, loading: true, error: null }))

			try {
				const response = await apiRequest(`/api/v1/accounts/${id}`, 'PUT', {})

				if (!response.ok) {
					throw new Error(`API error: ${response.status}`)
				}
				const response2 = await apiRequest(`/api/v1/accounts/${id}`, 'GET')
				if (!response2.ok) {
					throw new Error(`API error: ${response.status}`)
				}
				const data = await response2.json()
				set({
					...initialState,
					currentStep: 0,
					id: data.userId,
					data: { clientId: data.clientId, userId: data.userId, name: '', preferences: initialState.data.preferences },
					loading: false
				})

				return response.id
			} catch (err: any) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to initialize account'
				}))
				throw err
			}
		},

		matchClient: async ({ id, ssn, birthdate, name }: { id: string; ssn: string; birthdate: string; name: string }) => {
			update(state => ({ ...state, loading: true, error: null }))

			try {
				const response = await apiRequest(`/api/v1/accounts/${id}`, 'PUT', {
					ssn, birthdate, name,
				})
				console.log('called matchClient', { ssn, birthdate, name })
				if (!response.ok) {
					throw new Error(`API error: ${response.status}`)
				}
				const response2 = await apiRequest(`/api/v1/accounts/${id}`, 'GET')
				if (!response2.ok) {
					throw new Error(`API error: ${response.status}`)
				}

				const data = await response2.json()
				set({
					...initialState,
					currentStep: 1,
					id: data.userId,
					data: { clientId: data.clientId, userId: data.userId, name: data.name, preferences: initialState.data.preferences },
					loading: false
				})

				return data
			} catch (err: any) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to initialize account'
				}))
				throw err
			}
		},

		// Load an existing account process
		load: async (id: string) => {
			update(state => ({ ...state, loading: true, error: null }))

			try {
				const response = await apiRequest(`/api/v1/accounts/${id}`, 'GET')

				set({
					id: response.id,
					currentStep: response.currentStep || 0,
					completedSteps: response.completedSteps || [],
					data: response.data || initialState.data,
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
						newData = { ...newData, ...stepData }
						break
					case 2:
						newData.preferences = { ...newData.preferences, ...stepData }
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
			let currentState: AccountState
			update(state => {
				currentState = state
				return state
			})

			try {
				// Prepare the patch data for the API
				const patchData = {
					currentStep: stepIndex + 1, // Moving to the next step
					completedSteps: [...new Set([...currentState!.completedSteps, stepIndex])],
					data: currentState!.data
				}

				// Make the PATCH request to update the account process
				const response = await apiRequest(
					`/api/v1/onboardings/${currentState!.id}`,
					'PATCH',
					patchData
				)

				// Update the store with the response
				update(state => ({
					...state,
					currentStep: response.currentStep || state.currentStep,
					completedSteps: response.completedSteps || state.completedSteps,
					data: response.data || state.data,
					isComplete: response.isComplete || state.isComplete,
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
			let currentState: AccountState
			update(state => {
				currentState = state
				return state
			})

			try {
				// Prepare the patch data for completion
				const patchData = {
					isComplete: true,
					completedSteps: [...new Set([...currentState!.completedSteps, 3])], // Add final step
					data: currentState!.data
				}

				// Make the PATCH request to complete the account
				const response = await apiRequest(
					`/api/v1/onboardings/${currentState!.id}`,
					'PATCH',
					patchData
				)

				// Update the store with the response
				update(state => ({
					...state,
					isComplete: true,
					completedSteps: response.completedSteps || state.completedSteps,
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
export const account = createAccountStore()