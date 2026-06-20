package action;

import control.GameState;
import event.GameEventType;
import java.util.Collections;
import java.util.List;
import mapData.MapType;
import view.GameDialogService;

/** Runs the map selection flow before game initialization. */
public class MapSelectionAction implements GameAction {
  private final MapType currentMapType;
  private MapType selectedMapType;

  /**
   * Creates a map selection action.
   *
   * @param currentMapType map to preselect in the dialog
   */
  public MapSelectionAction(MapType currentMapType) {
    this.currentMapType = currentMapType;
  }

  /** Lets the user choose a map and stores the selection. */
  @Override
  public List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService) {
    MapType defaultMapType = currentMapType == null ? gameState.getMapType() : currentMapType;
    selectedMapType = gameDialogService.selectMapType(defaultMapType);
    return Collections.emptyList();
  }

  /**
   * Returns the map selected by the player.
   *
   * @return selected map type, or null when cancelled
   */
  public MapType getSelectedMapType() {
    return selectedMapType;
  }
}
