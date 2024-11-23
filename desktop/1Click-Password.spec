# -*- mode: python ; coding: utf-8 -*-


a = Analysis(
    ['1Click-Password.py'],
    pathex=[],
    binaries=[],
    datas=[('C:\\\\Users\\\\oreon\\\\AppData\\\\Local\\\\Programs\\\\Python\\\\Python313\\\\Lib\\\\site-packages\\\\pyperclip\\\\__init__.py', 'pyperclip')],
    hiddenimports=[],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    noarchive=False,
    optimize=0,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name='1Click-Password',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon=['icon.ico'],
)
