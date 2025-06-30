import { writable } from 'svelte/store';
import { nanoid} from "nanoid";
import {data} from "autoprefixer";
// Initialize the store with default values
const createRegistrationsStore = () => {
	const initialState = {
		data: {
			id: null,
			email: null,
			token: null,
		},
		isComplete: false,
		error: null,
		loading: false
	};

	const { subscribe, set, update } = writable(initialState);

	// Helper function to make API requests
	async function apiRequest(url, method, data = null) {
		try {
			url = 'http://localhost:3030' + url;
			const options = {
				method,
				headers: {
					'Content-Type': 'application/json'
				}
			};

			if (data) {
				options.body = JSON.stringify(data);
			}

			const response = await fetch(url, options);

			if (!response.ok) {
				throw new Error(`API error: ${response.status}`);
			}
			return response;

		} catch (err) {
			console.error('API request failed:', err);
			throw err;
		}
	}

	return {
		subscribe,

		// Initialize a new application process
		register: async ({ email }) => {
			update(state => ({ ...state, loading: true, error: null }));
			const id = nanoid();
			try {
				const response = await apiRequest(`/api/v1/registrations/${id}`, 'PUT', {
					email
				});


				if(!response.ok) {
					throw new Error(`API error: ${response.status}`);
				}

				var d = await apiRequest(`/api/v1/registrations/${id}`, 'GET');
				if(!d.ok) {
					throw new Error(`API error: ${response.status}`);
				}
				var newData = await d.json();
				console.log('NEW DATA', newData)
				set({
					...initialState,
					data: { id, email: newData.email, userId: newData.userId, token: newData.token},
					loading: false
				});
				return newData;
			} catch (err) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to register'
				}));
				throw err;
			}
		},
		authorize: async ({ id, code }) => {
			update(state => ({ ...state, loading: true, error: null }));
			try {
				const response = await apiRequest(`/api/v1/registrations/${id}/authorizations/${code}`, 'PUT');
				if(!response.ok) {
					throw new Error(`API error: ${response.status}`);
				}

				var d = await apiRequest(`/api/v1/registrations/${id}`, 'GET');
				if(!d.ok) {
					throw new Error(`API error: ${response.status}`);
				}
				var newData = await d.json();
				console.log('NEW DATA', newData)
				set({
					...initialState,
					data: { id, email: newData.email, userId: newData.userId, token: newData.token},
					loading: false
				});

				return newData;
			} catch (err) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to register'
				}));
				throw err;
			}
		},

		// Load an existing application process
		load: async (id) => {
			update(state => ({ ...state, loading: true, error: null }));

			try {
				const response = await apiRequest(`/api/v1/registrations/${id}`, 'GET');
				var data = await response.json();
				set({
					id: response.id,
					currentStep: response.currentStep || 0,
					completedSteps: response.completedSteps || [],
					data: { ...data, token: data.token },
					isComplete: response.isComplete || false,
					error: null,
					loading: false
				});

				return response;
			} catch (err) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to load application data'
				}));
				throw err;
			}
		},

		// Save data for the current step
		saveStep: async (stepIndex, stepData) => {
			update(state => {
				let newData = { ...state.data };

				// Map step index to data sections
				switch (stepIndex) {
					case 1:
						newData.personalInfo = { ...newData.personalInfo, ...stepData };
						break;
					case 2:
						newData.preferences = { ...newData.preferences, ...stepData };
						break;
				}

				return {
					...state,
					data: newData,
					loading: true,
					error: null
				};
			});

			// Get current state for API request
			let currentState;
			update(state => {
				currentState = state;
				return state;
			});

			try {
				// Prepare the patch data for the API
				const patchData = {
					currentStep: stepIndex + 1, // Moving to the next step
					completedSteps: [...new Set([...currentState.completedSteps, stepIndex])],
					data: currentState.data
				};

				// Make the PATCH request to update the application process
				const response = await apiRequest(
					`/api/v1/onboardings/${currentState.id}`,
					'PATCH',
					patchData
				);

				// Update the store with the response
				update(state => ({
					...state,
					currentStep: response.currentStep || state.currentStep,
					completedSteps: response.completedSteps || state.completedSteps,
					data: response.data || state.data,
					isComplete: response.isComplete || state.isComplete,
					loading: false
				}));

				return response;
			} catch (err) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to save step data'
				}));
				throw err;
			}
		},

		// Complete the application process
		complete: async () => {
			update(state => ({ ...state, loading: true, error: null }));

			// Get current state for API request
			let currentState;
			update(state => {
				currentState = state;
				return state;
			});

			try {
				// Prepare the patch data for completion
				const patchData = {
					isComplete: true,
					completedSteps: [...new Set([...currentState.completedSteps, 3])], // Add final step
					data: currentState.data
				};

				// Make the PATCH request to complete the application
				const response = await apiRequest(
					`/api/v1/onboardings/${currentState.id}`,
					'PATCH',
					patchData
				);

				// Update the store with the response
				update(state => ({
					...state,
					isComplete: true,
					completedSteps: response.completedSteps || state.completedSteps,
					loading: false
				}));

				return response;
			} catch (err) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to complete application'
				}));
				throw err;
			}
		},

		// Reset the store to initial state
		reset: () => set(initialState)
	};
};

// Create and export the store
export const registrations = createRegistrationsStore();
