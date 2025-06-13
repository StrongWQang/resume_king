declare const PDFKit: {
  new (options?: {
    size?: [number, number]
    margin?: number
    [key: string]: any
  }): PDFKitDocument
}

interface PDFKitDocument {
  pipe(stream: any): any
  image(src: string, x: number, y: number, options?: {
    width?: number
    height?: number
    [key: string]: any
  }): this
  end(): void
}

declare function blobStream(): {
  on(event: string, callback: (error?: Error) => void): void
  toBlob(type: string): Blob
}

declare global {
  interface Window {
    PDFKit: any
    blobStream: any
  }
} 