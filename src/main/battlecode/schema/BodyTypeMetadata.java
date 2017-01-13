// automatically generated by the FlatBuffers compiler, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * Metadata about all bodies of a particular type.
 */
public final class BodyTypeMetadata extends Table {
  public static BodyTypeMetadata getRootAsBodyTypeMetadata(ByteBuffer _bb) { return getRootAsBodyTypeMetadata(_bb, new BodyTypeMetadata()); }
  public static BodyTypeMetadata getRootAsBodyTypeMetadata(ByteBuffer _bb, BodyTypeMetadata obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public BodyTypeMetadata __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /**
   * The relevant type.
   */
  public byte type() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  /**
   * The radius of the type, in distance units.
   */
  public float radius() { int o = __offset(6); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * The cost of the type, in bullets.
   */
  public float cost() { int o = __offset(8); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * The maxiumum health of the type, in health units.
   */
  public float maxHealth() { int o = __offset(10); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * If unset, the same as maxHealth.
   * Otherwise, the health a body of this type starts with.
   */
  public float startHealth() { int o = __offset(12); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * The maximum distance this type can move each turn
   */
  public float strideRadius() { int o = __offset(14); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * The speed that bullets from this unit move.
   * Note: you don't need to keep track of this, SpawnedBody.vel will always be set.
   */
  public float bulletSpeed() { int o = __offset(16); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * The damage that bullets from this unit inflict.
   * Note: you don't need to keep track of this.
   */
  public float bulletAttack() { int o = __offset(18); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * The maximum distance this type can sense other trees and robots
   */
  public float sightRadius() { int o = __offset(20); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  /**
   * The maximum distance this type can sense bullets
   */
  public float bulletSightRadius() { int o = __offset(22); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }

  public static int createBodyTypeMetadata(FlatBufferBuilder builder,
      byte type,
      float radius,
      float cost,
      float maxHealth,
      float startHealth,
      float strideRadius,
      float bulletSpeed,
      float bulletAttack,
      float sightRadius,
      float bulletSightRadius) {
    builder.startObject(10);
    BodyTypeMetadata.addBulletSightRadius(builder, bulletSightRadius);
    BodyTypeMetadata.addSightRadius(builder, sightRadius);
    BodyTypeMetadata.addBulletAttack(builder, bulletAttack);
    BodyTypeMetadata.addBulletSpeed(builder, bulletSpeed);
    BodyTypeMetadata.addStrideRadius(builder, strideRadius);
    BodyTypeMetadata.addStartHealth(builder, startHealth);
    BodyTypeMetadata.addMaxHealth(builder, maxHealth);
    BodyTypeMetadata.addCost(builder, cost);
    BodyTypeMetadata.addRadius(builder, radius);
    BodyTypeMetadata.addType(builder, type);
    return BodyTypeMetadata.endBodyTypeMetadata(builder);
  }

  public static void startBodyTypeMetadata(FlatBufferBuilder builder) { builder.startObject(10); }
  public static void addType(FlatBufferBuilder builder, byte type) { builder.addByte(0, type, 0); }
  public static void addRadius(FlatBufferBuilder builder, float radius) { builder.addFloat(1, radius, 0.0f); }
  public static void addCost(FlatBufferBuilder builder, float cost) { builder.addFloat(2, cost, 0.0f); }
  public static void addMaxHealth(FlatBufferBuilder builder, float maxHealth) { builder.addFloat(3, maxHealth, 0.0f); }
  public static void addStartHealth(FlatBufferBuilder builder, float startHealth) { builder.addFloat(4, startHealth, 0.0f); }
  public static void addStrideRadius(FlatBufferBuilder builder, float strideRadius) { builder.addFloat(5, strideRadius, 0.0f); }
  public static void addBulletSpeed(FlatBufferBuilder builder, float bulletSpeed) { builder.addFloat(6, bulletSpeed, 0.0f); }
  public static void addBulletAttack(FlatBufferBuilder builder, float bulletAttack) { builder.addFloat(7, bulletAttack, 0.0f); }
  public static void addSightRadius(FlatBufferBuilder builder, float sightRadius) { builder.addFloat(8, sightRadius, 0.0f); }
  public static void addBulletSightRadius(FlatBufferBuilder builder, float bulletSightRadius) { builder.addFloat(9, bulletSightRadius, 0.0f); }
  public static int endBodyTypeMetadata(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

