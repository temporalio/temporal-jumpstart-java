

export const index = 4;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/registrations/_id_/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/4.d3970eba.js","_app/immutable/chunks/scheduler.448ae30c.js","_app/immutable/chunks/index.92fb6241.js","_app/immutable/chunks/registrations.32ea0031.js","_app/immutable/chunks/index.690509db.js","_app/immutable/chunks/stores.90cb9237.js","_app/immutable/chunks/singletons.7a2c55fc.js"];
export const stylesheets = [];
export const fonts = [];
