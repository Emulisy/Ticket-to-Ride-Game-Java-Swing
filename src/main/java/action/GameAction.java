package action;

import control.GameState;
import event.GameEventType;
import java.util.List;
import view.GameDialogService;

public interface GameAction {

  List<GameEventType> execute(GameState gameState, GameDialogService gameDialogService);
}
