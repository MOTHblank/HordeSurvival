import re
with open("app/src/main/java/com/hordesurvival/game/hazard/StageHazardSystem.kt", "r") as f:
    content = f.read()

hazard_logic = """            var entity: Entity? = null
            val activeEntities = engine.cachedActiveEntities
            for (i in 0 until activeEntities.size) {
                val e = activeEntities[i]
                if (e.id == hazard.entityId) {
                    entity = e
                    break
                }
            }"""

content = content.replace(hazard_logic, "            val entity = engine.getEntityById(hazard.entityId)")

with open("app/src/main/java/com/hordesurvival/game/hazard/StageHazardSystem.kt", "w") as f:
    f.write(content)
