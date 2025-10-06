
// this file is generated — do not edit it


/// <reference types="@sveltejs/kit" />

/**
 * Environment variables [loaded by Vite](https://vitejs.dev/guide/env-and-mode.html#env-files) from `.env` files and `process.env`. Like [`$env/dynamic/private`](https://kit.svelte.dev/docs/modules#$env-dynamic-private), this module cannot be imported into client-side code. This module only includes variables that _do not_ begin with [`config.kit.env.publicPrefix`](https://kit.svelte.dev/docs/configuration#env) _and do_ start with [`config.kit.env.privatePrefix`](https://kit.svelte.dev/docs/configuration#env) (if configured).
 * 
 * _Unlike_ [`$env/dynamic/private`](https://kit.svelte.dev/docs/modules#$env-dynamic-private), the values exported from this module are statically injected into your bundle at build time, enabling optimisations like dead code elimination.
 * 
 * ```ts
 * import { API_KEY } from '$env/static/private';
 * ```
 * 
 * Note that all environment variables referenced in your code should be declared (for example in an `.env` file), even if they don't have a value until the app is deployed:
 * 
 * ```
 * MY_FEATURE_FLAG=""
 * ```
 * 
 * You can override `.env` values from the command line like so:
 * 
 * ```bash
 * MY_FEATURE_FLAG="enabled" npm run dev
 * ```
 */
declare module '$env/static/private' {
	export const __CFBundleIdentifier: string;
	export const SHLVL: string;
	export const HOME: string;
	export const OBJC_DISABLE_INITIALIZE_FORK_SAFETY: string;
	export const MOCHA_COLORS: string;
	export const __CF_USER_TEXT_ENCODING: string;
	export const JAVA_HOME: string;
	export const ASDF_DIRENV_BIN: string;
	export const GOPRIVATE: string;
	export const PWD: string;
	export const HOMEBREW_CELLAR: string;
	export const ASDF_DIR: string;
	export const LANG: string;
	export const COLORTERM: string;
	export const NODE_ENV: string;
	export const XPC_SERVICE_NAME: string;
	export const GRANTED_ENABLE_AUTO_REASSUME: string;
	export const PATH: string;
	export const IJ_RESTARTER_LOG: string;
	export const DOTNET_ROOT: string;
	export const GOROOT: string;
	export const HOMEBREW_PREFIX: string;
	export const LIGHT_THEME: string;
	export const USER: string;
	export const npm_config_color: string;
	export const INFOPATH: string;
	export const ASDF_INSTALL_TYPE: string;
	export const ASDF_INSTALL_VERSION: string;
	export const ASDF_INSTALL_PATH: string;
	export const DARK_THEME: string;
	export const SSH_AUTH_SOCK: string;
	export const XPC_FLAGS: string;
	export const FORCE_COLOR: string;
	export const DEBUG_COLORS: string;
	export const LOGNAME: string;
	export const SHELL: string;
	export const HOMEBREW_REPOSITORY: string;
	export const TMPDIR: string;
	export const COMMAND_MODE: string;
	export const AWS_PROFILE: string;
}

/**
 * Similar to [`$env/static/private`](https://kit.svelte.dev/docs/modules#$env-static-private), except that it only includes environment variables that begin with [`config.kit.env.publicPrefix`](https://kit.svelte.dev/docs/configuration#env) (which defaults to `PUBLIC_`), and can therefore safely be exposed to client-side code.
 * 
 * Values are replaced statically at build time.
 * 
 * ```ts
 * import { PUBLIC_BASE_URL } from '$env/static/public';
 * ```
 */
declare module '$env/static/public' {
	
}

/**
 * This module provides access to runtime environment variables, as defined by the platform you're running on. For example if you're using [`adapter-node`](https://github.com/sveltejs/kit/tree/master/packages/adapter-node) (or running [`vite preview`](https://kit.svelte.dev/docs/cli)), this is equivalent to `process.env`. This module only includes variables that _do not_ begin with [`config.kit.env.publicPrefix`](https://kit.svelte.dev/docs/configuration#env) _and do_ start with [`config.kit.env.privatePrefix`](https://kit.svelte.dev/docs/configuration#env) (if configured).
 * 
 * This module cannot be imported into client-side code.
 * 
 * ```ts
 * import { env } from '$env/dynamic/private';
 * console.log(env.DEPLOYMENT_SPECIFIC_VARIABLE);
 * ```
 * 
 * > In `dev`, `$env/dynamic` always includes environment variables from `.env`. In `prod`, this behavior will depend on your adapter.
 */
declare module '$env/dynamic/private' {
	export const env: {
		__CFBundleIdentifier: string;
		SHLVL: string;
		HOME: string;
		OBJC_DISABLE_INITIALIZE_FORK_SAFETY: string;
		MOCHA_COLORS: string;
		__CF_USER_TEXT_ENCODING: string;
		JAVA_HOME: string;
		ASDF_DIRENV_BIN: string;
		GOPRIVATE: string;
		PWD: string;
		HOMEBREW_CELLAR: string;
		ASDF_DIR: string;
		LANG: string;
		COLORTERM: string;
		NODE_ENV: string;
		XPC_SERVICE_NAME: string;
		GRANTED_ENABLE_AUTO_REASSUME: string;
		PATH: string;
		IJ_RESTARTER_LOG: string;
		DOTNET_ROOT: string;
		GOROOT: string;
		HOMEBREW_PREFIX: string;
		LIGHT_THEME: string;
		USER: string;
		npm_config_color: string;
		INFOPATH: string;
		ASDF_INSTALL_TYPE: string;
		ASDF_INSTALL_VERSION: string;
		ASDF_INSTALL_PATH: string;
		DARK_THEME: string;
		SSH_AUTH_SOCK: string;
		XPC_FLAGS: string;
		FORCE_COLOR: string;
		DEBUG_COLORS: string;
		LOGNAME: string;
		SHELL: string;
		HOMEBREW_REPOSITORY: string;
		TMPDIR: string;
		COMMAND_MODE: string;
		AWS_PROFILE: string;
		[key: `PUBLIC_${string}`]: undefined;
		[key: `${string}`]: string | undefined;
	}
}

/**
 * Similar to [`$env/dynamic/private`](https://kit.svelte.dev/docs/modules#$env-dynamic-private), but only includes variables that begin with [`config.kit.env.publicPrefix`](https://kit.svelte.dev/docs/configuration#env) (which defaults to `PUBLIC_`), and can therefore safely be exposed to client-side code.
 * 
 * Note that public dynamic environment variables must all be sent from the server to the client, causing larger network requests — when possible, use `$env/static/public` instead.
 * 
 * ```ts
 * import { env } from '$env/dynamic/public';
 * console.log(env.PUBLIC_DEPLOYMENT_SPECIFIC_VARIABLE);
 * ```
 */
declare module '$env/dynamic/public' {
	export const env: {
		[key: `PUBLIC_${string}`]: string | undefined;
	}
}
