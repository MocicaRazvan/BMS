export class CustomAbortController extends AbortController {
  private additionalOnAbort: (() => void) | undefined;

  constructor(additionalOnAbort?: () => void) {
    super();
    this.additionalOnAbort = additionalOnAbort;
  }

  override abort(shouldCallAdditionalOnAbort = true, reason?: any): void {
    try {
      if (!this.signal.aborted) {
        super.abort(reason);
        if (shouldCallAdditionalOnAbort && this.additionalOnAbort) {
          this.additionalOnAbort();
        }
      }
    } catch (e) {
      console.log("CustomAbortController abort error", e);
    }
  }

  setAdditionalOnAbort(callback: () => void): void {
    const previous = this.additionalOnAbort;
    this.additionalOnAbort = () => {
      previous?.();
      callback();
    };
  }
  setAdditionalAbortFromFetch(
    fetchFunction:
      | AsyncGenerator
      | (AsyncGenerator & {
          abort: unknown;
        }),
  ) {
    if ("abort" in fetchFunction && typeof fetchFunction.abort === "function") {
      this.setAdditionalOnAbort(() => {
        (fetchFunction as any).abort?.();
      });
    }
  }
}
