#!/usr/bin/env python3
import argparse, base64, hashlib, json
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PublicKey
p=argparse.ArgumentParser();p.add_argument('report');p.add_argument('--public-key-base64',required=True);a=p.parse_args()
r=json.load(open(a.report,encoding='utf-8'));payload=r['payloadJson'].encode();expected=r['payloadSha256'];actual=hashlib.sha256(payload).hexdigest()
if actual!=expected: raise SystemExit('FAIL: SHA-256 diverge')
pub=Ed25519PublicKey.from_public_bytes(base64.b64decode(a.public_key_base64));pub.verify(base64.b64decode(r['signatureBase64']),payload);print('OK: hash e assinatura Ed25519 válidos')
