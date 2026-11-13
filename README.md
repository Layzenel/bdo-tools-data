# BDO Tools Data

Данные для [BDO Tools](https://github.com/Layzenel/Black-Desert-Online-Tools), которые собираются автоматически
и раздаются через GitHub Pages.

| Файл | Адрес | Что внутри |
|---|---|---|
| `docs/skills.json` | https://layzenel.github.io/bdo-tools-data/skills.json | Справочник активных навыков классов (с перезарядкой): `SkillNo` → классы, стойка, ранг, названия RU/EN, иконка |
| `docs/skills-no-cooldown.json` | https://layzenel.github.io/bdo-tools-data/skills-no-cooldown.json | Активные навыки без перезарядки («Flow:», удары с движением) в том же формате: их нет в слотах перезарядки, но игра пишет их в историю использования навыков. Пассивных навыков (команда `Passive`) нет ни в одном файле |
| `docs/skills/<SkillNo>.json` | `https://layzenel.github.io/bdo-tools-data/skills/<SkillNo>.json` | Тексты тултипа навыка последнего ранга на русском и английском |
| `docs/assets/…` | `https://layzenel.github.io/bdo-tools-data` + поле `icon` | Иконки навыков (webp) |

Данные о навыках и иконки взяты из открытого API и CDN [bdolytics.com](https://bdolytics.com) (конструктор навыков).
Иконки — игровые ресурсы Black Desert © Pearl Abyss.

## Формат `skills.json`

Объект, ключ — `SkillNo` (номер навыка в файлах игры), по записи на строку:

```json
"5338":{"classes":["striker"],"stance":"succession","max":true,"en":"Prime: Knee Hammer III","ru":"Мощь: Коленный молот III","icon":"/assets/…/pcm_skill_5336.webp"}
```

- `classes` — ключи классов (`CharacterClass.key` в приложении). Бывает несколько: общие навыки пар классов
  и навыки всех классов. Новый класс, которого ещё нет в таблице, получает временный ключ `class-<номер>`.
- `stance` — стойка: `succession` (наследие: «Prime:», требуют пассивку «Succession: …»), `awakening`
  (дерево пробуждения); нет поля — обе (основные навыки, «Absolute:», «Flow:»).
- `max` — последний ранг: нет ни следующего ранга в клетке дерева, ни «Absolute:» этого навыка; нет поля — не последний.
- `primeVariant` — у навыка «Absolute:» есть вариант наследия «Prime:» в том же классе: по строке «Skills you
  cannot learn after learning this skill» в описании «Prime:» или по паре названий. Персонажу наследия он не нужен.
- Навыки, которые пропали из bdolytics, из справочника не удаляются: их номера ещё лежат в файлах игроков.
  Предлагать их уже не нужно, поэтому `max` у них снимается.
- Общие навыки всех классов из быстрого слота («Гнев Черного духа», «V: Благословение Асадаля», «Инферно: Удар
  пальмары») игра не даёт поставить в слот перезарядки: `max` у них тоже снят. «Резкое уклонение» на своей клавише остаётся.

## Формат `skills/<SkillNo>.json`

`{"ru": {…}, "en": {…}}` — поля ответа `database.getSkill` bdolytics как есть: `name`, `description`,
`requiredSkillPoints`, `requiredLevel`, `requiredMp`, `requiredSp`, `cooldown` (мс), `command`, `tooltip`
(эффекты по группам). Цвет в тексте — теги `<span style="color: #…">`, клавиши — `{KeyBind:<действие>}`
(как `InputType` в `ShortcutKey_Action_Key` файла персонажа). Нулевые и пустые поля не пишутся.

## Как обновляется

`.github/workflows/update-data.yml` каждый день в 05:17 UTC (и вручную) собирает справочник
(`./gradlew run --args="docs/skills.json"`), докачивает иконки и подробности новых навыков, которых ещё нет
в `docs/`, и коммитит изменения; по четвергам (после патча) подробности перекачиваются все. Запросы подробностей
идут по одному с паузой, чтобы не упираться в ограничение bdolytics (HTTP 429). Старые навыки, иконки и подробности
не удаляются. Если bdolytics ответил подозрительно мало навыков, файл не трогается и запуск падает.
Новый класс открывает issue.
