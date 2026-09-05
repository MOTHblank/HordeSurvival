import re

file_path = "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/HitFlashSystem.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace("override fun update(dt: Float, entities: List<Entity>)", "override fun update(dt: Float, entities: com.badlogic.gdx.utils.Array<Entity>)")
content = content.replace("for (i in 0 until entities.size)", "for (i in 0 until entities.size)")
content = content.replace("val entity = entities[i]", "val entity = entities.get(i)")

with open(file_path, "w") as f:
    f.write(content)
print("done")
