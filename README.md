# BDO Tools Data

Данные для [BDO Tools](https://github.com/Layzenel/Black-Desert-Online-Tools), которые собираются автоматически
и раздаются через GitHub Pages.

| Файл | Адрес | Что внутри |
|---|---|---|
| `docs/skills.json` | https://layzenel.github.io/bdo-tools-data/skills.json | Справочник активных навыков классов (с перезарядкой): `SkillNo` → классы, ветка, названия RU/EN, иконка |
| `docs/assets/…` | `https://layzenel.github.io/bdo-tools-data` + поле `icon` | Иконки навыков (webp) |

Данные о навыках и иконки взяты из открытого API и CDN [bdolytics.com](https://bdolytics.com) (конструктор навыков).
Иконки — игровые ресурсы Black Desert © Pearl Abyss.

## Формат `skills.json`

Объект, ключ — `SkillNo` (номер навыка в файлах игры), по записи на строку:

```json
"5338":{"classes":["striker"],"tree":"succession","en":"Prime: Knee Hammer III","ru":"Мощь: Коленный молот III","icon":"/assets/…/pcm_skill_5336.webp"}
```

- `classes` — ключи классов (`CharacterClass.key` в приложении). Бывает несколько: общие навыки пар классов
  и навыки всех классов. Новый класс, которого ещё нет в таблице, получает временный ключ `class-<номер>`.
- `tree` — `succession` (наследие и основные навыки) или `awakening` (пробуждение).
- Навыки, которые пропали из bdolytics, из справочника не удаляются: их номера ещё лежат в файлах игроков.

## Как обновляется

`.github/workflows/update-data.yml` каждый день в 05:17 UTC (и вручную) собирает справочник
(`./gradlew run --args="docs/skills.json"`), докачивает иконки, которых ещё нет в `docs/`, и коммитит
изменения. Старые навыки и иконки не удаляются. Если bdolytics ответил
подозрительно мало навыков, файл не трогается и запуск падает. Новый класс открывает issue.
