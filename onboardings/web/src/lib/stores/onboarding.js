import { writable } from 'svelte/store';

// Initialize the store with default values
const createOnboardingStore = () => {
	const initialState = {
		id: null,
		currentStep: 0,
		completedSteps: [],
		data: {
			email: '', // Initial email from landing page
			personalInfo: {
				name: '',
				email: ''
			},
			preferences: {
				theme: 'light',
				enableNotifications: false
			}
		},
		isComplete: false,
		error: null,
		loading: false
	};

	const { subscribe, set, update } = writable(initialState);

	// Helper function to make API requests
	async function apiRequest(url, method, data = null) {
		try {
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

			return await response.json();
		} catch (err) {
			console.error('API request failed:', err);
			throw err;
		}
	}

	return {
		subscribe,

		// Initialize a new onboarding process
		initialize: async () => {
			update(state => ({ ...state, loading: true, error: null }));

			try {
				const response = await apiRequest('/api/v1/onboardings', 'POST');

				set({
					...initialState,
					id: response.id,
					loading: false
				});

				return response.id;
			} catch (err) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to initialize onboarding'
				}));
				throw err;
			}
		},

		// Load an existing onboarding process
		load: async (id) => {
			update(state => ({ ...state, loading: true, error: null }));

			try {
				const response = await apiRequest(`/api/v1/onboardings/${id}`, 'GET');

				set({
					id: response.id,
					currentStep: response.currentStep || 0,
					completedSteps: response.completedSteps || [],
					data: response.data || initialState.data,
					isComplete: response.isComplete || false,
					error: null,
					loading: false
				});

				return response;
			} catch (err) {
				update(state => ({
					...state,
					loading: false,
					error: err.message || 'Failed to load onboarding data'
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

				// Make the PATCH request to update the onboarding process
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

		// Complete the onboarding process
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

				// Make the PATCH request to complete the onboarding
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
					error: err.message || 'Failed to complete onboarding'
				}));
				throw err;
			}
		},

		// Reset the store to initial state
		reset: () => set(initialState)
	};
};

// Create and export the store
export const onboarding = createOnboardingStore();
