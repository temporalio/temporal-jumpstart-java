import * as universal from '../entries/pages/_layout.ts.js';

export const index = 0;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/_layout.svelte.js')).default;
export { universal };
export const universal_id = "src/routes/+layout.ts";
export const imports = ["_app/immutable/nodes/0.a729c573.js","_app/immutable/chunks/scheduler.448ae30c.js","_app/immutable/chunks/index.92fb6241.js","_app/immutable/chunks/index.690509db.js"];
export const stylesheets = ["_app/immutable/assets/0.95069a23.css"];
export const fonts = [];
