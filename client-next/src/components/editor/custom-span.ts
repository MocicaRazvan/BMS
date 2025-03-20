import { Node } from "@tiptap/core";

export const CustomSpan = Node.create({
  name: "customSpan",
  inline: true,
  group: "inline",
  content: "inline*",
  isolating: true,

  addAttributes() {
    return {
      style: {
        default: null,
        parseHTML: (element) => element.getAttribute("style"),
        renderHTML: (attributes) => ({
          style: attributes.style,
        }),
      },
    };
  },

  parseHTML() {
    return [{ tag: "span" }];
  },

  renderHTML({ node }) {
    return ["span", { style: node.attrs.style }, 0];
  },
});
