package me.earth.phobot.modules.client.anticheat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.earth.phobot.movement.BunnyHop;
import me.earth.phobot.movement.BunnyHopCC;
import me.earth.phobot.movement.BunnyHopNcp;
import me.earth.phobot.movement.Movement;

@Getter
@RequiredArgsConstructor
public enum MovementAntiCheat {
    Vanilla(new BunnyHop()), // TODO: vanilla bhop is just 3arth 1.12.2 bhop
    Ncp(new BunnyHopNcp()),
    CC(new BunnyHopCC()),
    Grim(new BunnyHopCC()); // TODO: for now, this needs a real one

    // TODO: should probably go into a separate bhop mode setting or something
    private final Movement movement;

}