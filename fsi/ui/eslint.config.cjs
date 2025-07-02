const globals = require( 'globals')

const pluginJs = require( '@eslint/js')

const importPlugin = require('eslint-plugin-import')

const tsEslint = require('typescript-eslint');

const stylistic = require('@stylistic/eslint-plugin')
// noinspection JSUnusedGlobalSymbols
export default [
  {
    files: ['**/*.{js,mjs,cjs,ts,json}'],
  },
  {
    languageOptions: { globals: globals.browser },
  },
  {
    plugins: {
      '@stylistic': stylistic,
    },
  },
  pluginJs.configs.recommended,
  ...tsEslint.configs.recommendedTypeChecked,
  importPlugin.flatConfigs.recommended,
  stylistic.configs.customize({
    semi: false,
  }),
  {
    languageOptions: {
      parserOptions: {
        projectService: {
          allowDefaultProject: ['*.js'],
          defaultProject: 'tsconfig.json',
        },
        tsconfigRootDir: '.',
      },
    },
  },
  {
    settings: {
      // from guidance here: https://github.com/import-js/eslint-plugin-import?tab=readme-ov-file#typescript
      'import/resolver': {
        // You will also need to install and configure the TypeScript resolver
        // See also https://github.com/import-js/eslint-import-resolver-typescript#configuration
        typescript: true,
        node: true,
      },
    },
    rules: {
      'eqeqeq': ['error', 'always', { null: 'ignore' }],
      'no-duplicate-imports': 'error',
      'object-shorthand': ['error', 'always'],
      '@typescript-eslint/no-deprecated': 'error',
      '@stylistic/semi': 'error',
    },
  },
]
