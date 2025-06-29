/** @type {import('@sveltejs/kit').Handle} */
export async function handle({ event, resolve }) {
	// Ignore requests to .well-known paths
	if (event.url.pathname.startsWith('/.well-known/')) {
		return new Response(null, { status: 204 });
	}

	return await resolve(event);
}
