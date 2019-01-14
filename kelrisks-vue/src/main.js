// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
// Font Awesome @ https://fontawesome.com/how-to-use/on-the-web/using-with/vuejs
import {library} from '@fortawesome/fontawesome-svg-core'
import {faBriefcase, faCheck, faChevronLeft, faChevronRight, faSearch, faSpinner, faTimes, faUndo, faUser} from '@fortawesome/free-solid-svg-icons'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
// Vue Material @ https://vuematerial.io/getting-started
import {MdInput} from 'vue-material/dist/components'
import 'vue-material/dist/vue-material.min.css'

library.add(faUser, faBriefcase, faChevronLeft, faChevronRight, faSearch, faSpinner, faUndo, faCheck, faTimes)
Vue.component('font-awesome-icon', FontAwesomeIcon)

Vue.use(MdInput)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: {App},
  template: '<App/>'
})
