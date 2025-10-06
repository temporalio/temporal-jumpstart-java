import * as universal from '../entries/pages/_page.ts.js';

export const index = 2;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/_page.svelte.js')).default;
export { universal };
export const universal_id = "src/routes/+page.ts";
export const imports = ["_app/immutable/nodes/2.f4f213ca.js","_app/immutable/chunks/scheduler.448ae30c.js","_app/immutable/chunks/index.92fb6241.js","_app/immutable/chunks/registrations.32ea0031.js","_app/immutable/chunks/index.690509db.js"];
export const stylesheets = [];
export const fonts = [];
