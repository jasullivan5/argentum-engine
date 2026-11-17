#!/usr/bin/env python3
"""Tests for scripts/card-list-status: a `# set: <CODE>` list is held to the set itself."""

import importlib.machinery
import importlib.util
from pathlib import Path
import unittest
from unittest import mock

SCRIPTS = Path(__file__).resolve().parent
_loader = importlib.machinery.SourceFileLoader("card_list_status", str(SCRIPTS / "card-list-status"))
status = importlib.util.module_from_spec(importlib.util.spec_from_loader("card_list_status", _loader))
_loader.exec_module(status)


class CardListStatusTest(unittest.TestCase):
    def test_set_directive_only_on_the_first_line(self):
        self.assertEqual(status.set_directive("# set: M21\nForest\n"), "m21")
        self.assertEqual(status.set_directive("\n#set:afr\n"), "afr")
        self.assertIsNone(status.set_directive("Forest\n# set: M21\n"))
        self.assertIsNone(status.set_directive("# my deck\nForest\n"))

    def test_set_list_counts_a_card_missing_until_the_set_has_it(self):
        with mock.patch.object(status, "set_missing_names", return_value={"forest", "solemn simulacrum"}) as m, \
                mock.patch.object(status, "_load_staples", side_effect=AssertionError("name-only check used")):
            names, missing = status.missing_cards("# set: M21\nForest\nSolemn Simulacrum\nBasri Ket\n")
        m.assert_called_once_with("m21")
        self.assertEqual(names, ["Forest", "Solemn Simulacrum", "Basri Ket"])
        self.assertEqual(missing, ["Forest", "Solemn Simulacrum"])

    def test_plain_list_checks_names_across_the_corpus(self):
        staples = mock.Mock()
        staples.implemented_names.return_value = {"sol ring"}
        staples.is_implemented.side_effect = lambda n, impl: n.lower() in impl
        with mock.patch.object(status, "_load_staples", return_value=staples), \
                mock.patch.object(status, "set_missing_names", side_effect=AssertionError("set check used")):
            names, missing = status.missing_cards("1 Sol Ring (C21) 263\n1 Dina, Soul Steeper\n")
        self.assertEqual(missing, ["Dina, Soul Steeper"])


if __name__ == "__main__":
    unittest.main()
