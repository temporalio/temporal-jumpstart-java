import type { Handle } from '@sveltejs/kit'

export const handle: Handle = async ({ event, resolve }) => {
	// Ignore requests to .well-known paths
	if (event.url.pathname.startsWith('/.well-known/')) {
		return new Response(null, { status: 204 })
	}

	return await resolve(event)
}