## Features
- Added EMI compatibility.
- Renamed Scarlet Pierogies to Scarlet Pierogi.
  - Items with the old ID will be converted to use the new ID.
    - The main caveat to this rule is that modded containers *may* not work. To workaround this, you should have any Pierogi in your inventory.
    - Do not report the above to any mod developers, it's unavoidable.
- Introduced DFU, currently used for converting `brewinandchewin:scarlet_pierogies` items to the new name.

## Bugfixes
- Fixed coaster item removals not being synced to nearby clients. ([#19](https://github.com/MerchantPug/BrewinAndChewin/issues/19))
- Fixed Kegs not prioritising the actual insertion item's crafting remainder items for recipes. ([#21](https://github.com/MerchantPug/BrewinAndChewin/issues/21))
- Fixed inconsistencies with inserting/extracting fluids within the Keg GUI. ([#25](https://github.com/MerchantPug/BrewinAndChewin/issues/25))
- Keg fluid item slot placement no longer operates each tick, it now only operates as soon as the item is put into the keg.

## Language
- Added Japanese localization ([#22](https://github.com/MerchantPug/BrewinAndChewin/pull/22))