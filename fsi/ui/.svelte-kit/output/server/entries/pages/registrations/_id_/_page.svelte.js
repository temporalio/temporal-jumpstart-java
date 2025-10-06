import { c as create_ssr_component, h as subscribe, a as add_attribute } from "../../../../chunks/ssr.js";
import { p as page } from "../../../../chunks/stores.js";
const Page = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let $$unsubscribe_page;
  $$unsubscribe_page = subscribe(page, (value) => value);
  let code = "";
  $$unsubscribe_page();
  return `<div class="container h-full mx-auto p-4"><div class="card w-full min-h-[90vh] grid grid-cols-1 md:grid-cols-2 overflow-hidden"> <div class="p-8 flex flex-col justify-center"><div class="max-w-lg mx-auto w-full"><h1 class="h1 mb-2" data-svelte-h="svelte-39cwyp">Check your email</h1> ${``} <form class="space-y-6"><label class="label"><span data-svelte-h="svelte-1xw5f77">Code</span> <input class="input" type="text" placeholder="Enter the code from your email" required${add_attribute("value", code, 0)}></label> <div class="flex flex-col space-y-4"><button type="submit" class="btn variant-filled-primary text-lg py-3" ${"disabled"}>${``}
                            Submit</button></div></form> <div class="mt-12" data-svelte-h="svelte-15klja7"><h3 class="h4 mb-4">Why join our platform?</h3> <ul class="list space-y-2"><li class="flex"><div class="mr-2">✓</div> <div>Build resilient applications with durable execution</div></li> <li class="flex"><div class="mr-2">✓</div> <div>Scale seamlessly with our distributed architecture</div></li> <li class="flex"><div class="mr-2">✓</div> <div>Access expert support and comprehensive documentation</div></li></ul></div></div></div>  <div class="hidden md:block bg-gradient-to-br from-primary-900 to-tertiary-900" data-svelte-h="svelte-1vcz10d"><div class="h-full flex flex-col justify-center items-center p-8 text-white"><img src="https://placehold.co/600x400/1e293b/ffffff?text=Temporal+Workflows" alt="Temporal Workflows" class="w-3/4 max-w-lg rounded-lg shadow-xl mb-8"> <h2 class="h2 text-center mb-4">Build Durable Applications</h2> <p class="text-lg text-center max-w-md">Temporal provides a developer-first platform for writing reliable applications without worrying about timeouts, retries, or infrastructure failures.</p></div></div></div></div>`;
});
export {
  Page as default
};
