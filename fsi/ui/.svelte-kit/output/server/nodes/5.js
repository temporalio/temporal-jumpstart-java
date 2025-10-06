

export const index = 5;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/pages/wealthmanagement/application/_page.svelte.js')).default;
export const imports = ["_app/immutable/nodes/5.6a39bca3.js","_app/immutable/chunks/scheduler.448ae30c.js","_app/immutable/chunks/index.92fb6241.js","_app/immutable/chunks/OnboardingStepIndicator.ac77b7fb.js"];
export const stylesheets = [];
export const fonts = [];
