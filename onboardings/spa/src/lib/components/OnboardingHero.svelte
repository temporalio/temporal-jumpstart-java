<script>
	export let currentStep;

	// Different content for each step
	const heroContent = {
		1: {
			title: 'Begin Your Journey',
			description: 'Start your application process with a few simple steps.',
			icon: '👋'
		},
		2: {
			title: 'Tell Us About Yourself',
			description: 'Help us personalize your experience.',
			icon: '👤'
		},
		3: {
			title: 'Make It Yours',
			description: 'Customize your experience to fit your needs.',
			icon: '⚙️'
		},
		4: {
			title: 'Ready to Go!',
			description: 'Your account is now fully configured.',
			icon: '🚀'
		}
	};
</script>

<div class="h-full flex flex-col justify-center items-center text-center space-y-8 p-10 bg-gradient-to-br from-primary-800 to-tertiary-800 text-white rounded-l-lg">
	<div class="text-8xl mb-6">{heroContent[currentStep.id].icon}</div>
	<h1 class="h1">{heroContent[currentStep.id].title}</h1>
	<p class="text-lg">{heroContent[currentStep.id].description}</p>

	<div class="mt-auto pt-8">
		<img src="https://placehold.co/400x200/1e293b/ffffff?text=Onboarding" alt="Onboarding illustration" class="w-full max-w-md mx-auto opacity-80" />
	</div>
</div>
