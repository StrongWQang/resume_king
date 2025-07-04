declare module 'pdfkit-browserify' {
  import PDFDocument from 'pdfkit';
  export = PDFDocument;
}

declare module 'blob-stream' {
  import { Writable } from 'stream';
  
  interface BlobStream extends Writable {
    toBlobURL: (type: string) => string;
  }
  
  const blobStream: () => BlobStream;
  export = blobStream;
} 