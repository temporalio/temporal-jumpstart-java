<script>
	// Skeleton
	import { AppShell } from '@skeletonlabs/skeleton';
	// Import global CSS styles
	import '../app.postcss';

</script>

<AppShell>
	<slot />
</AppShell>
<AppShell>
	<main class="h-full">
		<slot />
	</main>
</AppShell>
