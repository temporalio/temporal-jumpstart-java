

export const index = 1;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/_error.svelte.js')).default;
export const imports = ["_app/immutable/nodes/1.2964e0f1.js","_app/immutable/chunks/scheduler.448ae30c.js","_app/immutable/chunks/index.92fb6241.js","_app/immutable/chunks/stores.90cb9237.js","_app/immutable/chunks/singletons.7a2c55fc.js","_app/immutable/chunks/index.690509db.js"];
export const stylesheets = [];
export const fonts = [];
