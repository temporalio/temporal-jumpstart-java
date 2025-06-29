<script>
	import { page } from '$app/stores';
</script>

<div class="container h-full mx-auto flex justify-center items-center">
	<div class="card p-8 shadow-xl">
		<h1 class="h1 mb-4">{$page.status}: {$page.error?.message || 'Not found'}</h1>
		<p class="mb-8">The page you're looking for doesn't exist or you don't have permission to view it.</p>
		<a href="/" class="btn variant-filled-primary">Go to Home</a>
	</div>
</div>
