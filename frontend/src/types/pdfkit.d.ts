declare module 'pdfkit' {
  export default class PDFDocument {
    constructor(options?: any);
    pipe(destination: any): any;
    image(data: any, x: number, y: number, options?: any): this;
    end(): void;
  }
}

declare module 'blob-stream' {
  interface IBlobStream extends NodeJS.WritableStream {
    toBlob(type?: string): Blob;
    toBlobURL(type?: string): string;
  }

  function blobStream(): IBlobStream;
  export default blobStream;
}

declare global {
  interface Window {
    PDFDocument: typeof PDFDocument;
    blobStream: typeof blobStream;
  }
} 