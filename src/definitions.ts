export interface IronSourcePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
