"use client";

import { useEffect, useRef } from "react";

interface RichTextEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

export default function RichTextEditor({ value, onChange, placeholder }: RichTextEditorProps) {
  const editorRef = useRef<HTMLDivElement>(null);
  const quillRef = useRef<any>(null);
  const isInternalChange = useRef(false);

  useEffect(() => {
    if (typeof window === "undefined" || !editorRef.current) return;
    if (quillRef.current) return; // already initialized

    import("quill").then((QuillModule) => {
      const Quill = QuillModule.default;

      // Import quill CSS once
      if (!document.querySelector("link[data-quill-css]")) {
        const link = document.createElement("link");
        link.rel = "stylesheet";
        link.href = "https://cdn.jsdelivr.net/npm/quill@2/dist/quill.snow.css";
        link.setAttribute("data-quill-css", "true");
        document.head.appendChild(link);
      }

      const quill = new Quill(editorRef.current!, {
        theme: "snow",
        placeholder: placeholder || "Write a description...",
        modules: {
          toolbar: [
            [{ header: [1, 2, 3, false] }],
            ["bold", "italic", "underline", "strike", "blockquote"],
            [{ list: "ordered" }, { list: "bullet" }],
            ["link", "clean"],
          ],
        },
      });

      quillRef.current = quill;

      // Set initial value
      if (value) {
        quill.clipboard.dangerouslyPasteHTML(value);
      }

      quill.on("text-change", () => {
        isInternalChange.current = true;
        onChange(quill.root.innerHTML);
        isInternalChange.current = false;
      });
    });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Sync external value changes (but not ones we triggered)
  useEffect(() => {
    if (quillRef.current && !isInternalChange.current) {
      const currentHTML = quillRef.current.root.innerHTML;
      if (currentHTML !== value && value !== undefined) {
        quillRef.current.clipboard.dangerouslyPasteHTML(value || "");
      }
    }
  }, [value]);

  return (
    <>
      <style>{`
        .ql-toolbar.ql-snow {
          border: none !important;
          border-bottom: 1px solid var(--border) !important;
          background-color: hsl(var(--secondary));
          border-radius: 0.75rem 0.75rem 0 0;
        }
        .ql-container.ql-snow {
          border: none !important;
          min-height: 250px;
          font-size: 1rem;
          color: hsl(var(--foreground));
        }
        .ql-editor {
          min-height: 250px;
        }
        .ql-snow .ql-stroke {
          stroke: hsl(var(--foreground)) !important;
        }
        .ql-snow .ql-fill, .ql-snow .ql-stroke.ql-fill {
          fill: hsl(var(--foreground)) !important;
        }
        .ql-snow .ql-picker {
          color: hsl(var(--foreground)) !important;
        }
        .ql-snow .ql-picker-options {
          background: hsl(var(--card)) !important;
          border-color: hsl(var(--border)) !important;
        }
        .ql-editor.ql-blank::before {
          color: hsl(var(--foreground) / 0.4) !important;
          font-style: normal;
        }
      `}</style>
      <div className="bg-secondary/50 rounded-xl overflow-hidden border border-border focus-within:border-primary transition-colors">
        <div ref={editorRef} />
      </div>
    </>
  );
}
