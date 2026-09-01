import re
with open("app/src/main/java/com/hordesurvival/game/hazard/StageHazardSystem.kt", "r") as f:
    content = f.read()

content = content.replace("val entity = engine.getActiveEntities().find { it.id == hazard.entityId }", "val entity = engine.getEntityById(hazard.entityId)")

with open("app/src/main/java/com/hordesurvival/game/hazard/StageHazardSystem.kt", "w") as f:
    f.write(content)
