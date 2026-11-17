import { describe, expect, it } from 'vitest'
import { parseManaDistributionPrompt } from './ManaDistributionUI'

describe('parseManaDistributionPrompt', () => {
  it('reads the two colors and the total of a mana split', () => {
    expect(parseManaDistributionPrompt('Choose how much {U} mana to add (rest will be {R}). Total: 5')).toEqual({
      firstColor: 'U',
      secondColor: 'R',
      total: 5,
      unitMultiplier: 1,
    })
  })

  it('reads the per-unit multiplier when mana is multiplied', () => {
    expect(
      parseManaDistributionPrompt(
        'Choose how much {G} mana to add (rest will be {R}). Total: 2 — each becomes 3 mana of that color',
      )?.unitMultiplier,
    ).toBe(3)
  })

  it('ignores other number prompts', () => {
    expect(parseManaDistributionPrompt('Choose a number')).toBeNull()
  })
})
