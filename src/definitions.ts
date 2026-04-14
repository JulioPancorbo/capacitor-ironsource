export interface IronSourcePlugin {
  /**
   * Inicializa el SDK de ironSource
   */
  initialize(options: { appKey: string }): Promise<void>;

  /**
   * Muestra un vídeo recompensado si está disponible
   */
  showRewardedVideo(): Promise<void>;
  
  /**
   * Comprueba si hay un vídeo recompensado listo para mostrar
   */
  isRewardedVideoAvailable(): Promise<{ available: boolean }>;
}