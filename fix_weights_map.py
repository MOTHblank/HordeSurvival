import re

file_path = "app/src/main/java/com/hordesurvival/ui/screens/mapselect/MapSelectScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

def replace_weight(match):
    return match.group(0).replace("Modifier.weight(1f)", "Modifier.weight(1f, fill = false)")

content = re.sub(r'HordeSecondaryButton\([\s\S]*?modifier\s*=\s*Modifier\.weight\(1f\)[\s\S]*?\)', replace_weight, content)
content = re.sub(r'HordeButton\([\s\S]*?modifier\s*=\s*Modifier\.weight\(1f\)[\s\S]*?\)', replace_weight, content)

with open(file_path, "w") as f:
    f.write(content)

print("Done MapSelect")
