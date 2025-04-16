declare module "crontzconvert" {
  export function convert(
    cron: string,
    originalTimezone: string,
    targetTimezone: string,
  ): string;
}
