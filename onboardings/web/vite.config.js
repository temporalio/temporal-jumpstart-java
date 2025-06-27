import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	server: {
		fs: {
			allow: ['..']
		},
		middleware: [
			(req, res, next) => {
				if (req.url?.startsWith('/.well-known/')) {
					res.statusCode = 204;
					res.end();
					return;
				}
				next();
			}
		]
	}
});
