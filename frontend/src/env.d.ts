/// <reference types="vite/client" />

declare module "*.vue" {
  import type { DefineComponent } from "vue";
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

declare module "vue-json-pretty" {
  import type { DefineComponent } from "vue";
  const VueJsonPretty: DefineComponent<
    {
      data?: any;
      showLength?: boolean;
      showLine?: boolean;
      highlightMouseoverNode?: boolean;
      highlightSelectedNode?: boolean;
    },
    {},
    any
  >;
  export default VueJsonPretty;
}

declare module "vue-json-pretty/lib/styles.css";
