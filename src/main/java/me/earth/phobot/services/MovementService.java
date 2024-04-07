package me.earth.phobot.services;

import lombok.RequiredArgsConstructor;
import me.earth.phobot.modules.client.anticheat.AntiCheat;
import me.earth.phobot.movement.Movement;

@RequiredArgsConstructor
public class MovementService {
    private final AntiCheat antiCheat;

    public Movement getMovement() {
        return antiCheat.getMovement().getValue().getMovement();
    }

}
