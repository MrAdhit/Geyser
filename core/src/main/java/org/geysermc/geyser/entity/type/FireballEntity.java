/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.entity.type;

import org.cloudburstmc.math.vector.Vector3f;
import org.geysermc.geyser.entity.EntityDefinition;
import org.geysermc.geyser.session.GeyserSession;

import java.util.UUID;

public class FireballEntity extends ThrowableEntity {
    private final Vector3f acceleration;

    /**
     * The number of ticks to advance movement before sending to Bedrock
     */
    protected int futureTicks = 3;

    public FireballEntity(GeyserSession session, int entityId, long geyserId, UUID uuid, EntityDefinition<?> definition, Vector3f position, Vector3f motion, float yaw, float pitch, float headYaw) {
        super(session, entityId, geyserId, uuid, definition, position, Vector3f.ZERO, yaw, pitch, headYaw);

        float magnitude = motion.length();
        if (magnitude != 0) {
            acceleration = motion.div(magnitude).mul(0.1f);
        } else {
            acceleration = Vector3f.ZERO;
        }
    }

    private Vector3f tickMovement(Vector3f position) {
        position = position.add(motion);
        float drag = getDrag();
        motion = motion.add(acceleration).mul(drag);
        return position;
    }

    @Override
    protected void moveAbsoluteImmediate(Vector3f position, float yaw, float pitch, float headYaw, boolean isOnGround, boolean teleported) {
        // Advance the position by a few ticks before sending it to Bedrock
        Vector3f lastMotion = motion;
        Vector3f newPosition = position;
        for (int i = 0; i < futureTicks; i++) {
            newPosition = tickMovement(newPosition);
        }
        super.moveAbsoluteImmediate(newPosition, yaw, pitch, headYaw, isOnGround, teleported);
        this.position = position;
        this.motion = lastMotion;
    }

    @Override
    public void tick() {
        if (removedInVoid()) {
            return;
        }
        moveAbsoluteImmediate(tickMovement(position), getYaw(), getPitch(), getHeadYaw(), false, false);
    }
}
