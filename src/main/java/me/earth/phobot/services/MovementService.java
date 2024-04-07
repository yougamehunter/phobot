package me.earth.phobot.services;

import lombok.Getter;
import me.earth.phobot.Phobot;
import me.earth.phobot.movement.BunnyHop;
import me.earth.phobot.movement.BunnyHopCC;
import me.earth.phobot.movement.BunnyHopNcp;
import me.earth.phobot.movement.Movement;

@Getter
public class MovementService {
    private Movement movement;

    public Movement getMovement(Phobot phobot) {
        switch(phobot.getAntiCheat().getMovement().getValue()) {
            case CC -> movement = new BunnyHopCC();
            case Ncp -> movement = new BunnyHopNcp();
            case Grim, Vanilla -> movement = new BunnyHop();
        }
        return movement;
    }

    public Movement getMovement() {
        return new BunnyHopCC();
    }

}
