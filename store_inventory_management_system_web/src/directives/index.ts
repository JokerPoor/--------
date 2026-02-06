import type { App } from 'vue'
import perm from './perm'

export default function registerDirectives(app: App) {
  app.directive('perm', perm)
}