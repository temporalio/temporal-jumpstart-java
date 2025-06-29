// Import tailwind plugins
import forms from '@tailwindcss/forms';
import typography from '@tailwindcss/typography';

// Import skeleton plugin creator
import { skeleton } from '@skeletonlabs/tw-plugin';

/** @type {import('tailwindcss').Config} */
export default {
	darkMode: 'class',
	content: [
		'./src/**/*.{html,js,svelte,ts}',
		'./node_modules/@skeletonlabs/skeleton/**/*.{html,js,svelte,ts}'
	],
	theme: {
		extend: {},
	},
	plugins: [
		forms,
		typography,
		skeleton({
			themes: {
				preset: [
					{
						name: 'modern',
						enabledIn: 'app'
					}
				]
			}
		})
	]
};
