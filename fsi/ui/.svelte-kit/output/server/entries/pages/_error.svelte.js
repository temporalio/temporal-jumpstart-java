import { c as create_ssr_component, h as subscribe, e as escape } from "../../chunks/ssr.js";
import { p as page } from "../../chunks/stores.js";
const Error = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let $page, $$unsubscribe_page;
  $$unsubscribe_page = subscribe(page, (value) => $page = value);
  $$unsubscribe_page();
  return `<div class="container h-full mx-auto flex justify-center items-center"><div class="card p-8 shadow-xl"><h1 class="h1 mb-4">${escape($page.status)}: ${escape($page.error?.message || "Not found")}</h1> <p class="mb-8" data-svelte-h="svelte-pcj061">The page you&#39;re looking for doesn&#39;t exist or you don&#39;t have permission to view it.</p> <a href="/" class="btn variant-filled-primary" data-svelte-h="svelte-g5v0pt">Go to Home</a></div></div>`;
});
export {
  Error as default
};
