# BDO Tools Data

Данные для [BDO Tools](https://github.com/Layzenel/Black-Desert-Online-Tools), которые собираются автоматически
и раздаются через GitHub Pages.

| Файл | Адрес | Что внутри |
|---|---|---|
| `docs/skills.json` | https://layzenel.github.io/bdo-tools-data/skills.json | Справочник навыков классов: `SkillNo` → классы, ветка, названия RU/EN, иконка |

Данные о навыках взяты из открытого API [bdolytics.com](https://bdolytics.com) (конструктор навыков).
Иконки лежат на `https://cdn.questlog.gg/black-desert` + путь из поля `icon`.

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
(`./gradlew run --args="docs/skills.json"`) и коммитит его, если он изменился. Если bdolytics ответил
подозрительно мало навыков, файл не трогается и запуск падает. Новый класс открывает issue.
