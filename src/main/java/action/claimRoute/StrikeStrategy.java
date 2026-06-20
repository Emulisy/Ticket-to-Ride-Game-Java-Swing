package action.claimRoute;

import control.GameState;
import model.City;
import model.Claimable;
import view.GameDialogService;

public class StrikeStrategy extends ClaimRouteStrategy {
  boolean canClaimRoute(
      Claimable claimable, GameState gameState, GameDialogService gameDialogService) {
    City strikeCity = gameState.getTurnManager().getStrikeCity();
    if (isConnectedToStrikeCity(claimable, strikeCity)) {
      gameDialogService.showMessage(
          "Claim Route",
          "Railway strike at "
              + strikeCity.getName()
              + ". Routes connected to it are unavailable.");
      return false;
    }
    return super.canClaimRoute(claimable, gameState, gameDialogService);
  }

  private boolean isConnectedToStrikeCity(Claimable claimable, City strikeCity) {
    return strikeCity != null
        && (strikeCity.equals(claimable.getCityA()) || strikeCity.equals(claimable.getCityB()));
  }
}
