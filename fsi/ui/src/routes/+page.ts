import type { PageLoad } from './$types'

// This marks the route as requiring client-side routing
export const csr = true

// Since we're disabling SSR, this load function runs only on the client
export const load: PageLoad = () => {
  return {
    // Any data you want to pass to the page
  }
}