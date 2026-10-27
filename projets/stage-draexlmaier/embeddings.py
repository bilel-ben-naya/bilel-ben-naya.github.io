from sentence_transformers import SentenceTransformer, util
from dotenv import load_dotenv
load_dotenv()

model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")

phrases = [
    "Mon écran ne s'allume plus",
    "L'écran de mon PC reste noir",
    "Je n'arrive plus à me connecter au VPN"
]
embedding1 = model.encode(phrases)[0]
embedding2 = model.encode(phrases)[1]
embedding3 = model.encode(phrases)[2]

print(f"{phrases[0]} vs {phrases[1]} : {util.cos_sim(embedding1,embedding2)} \n{phrases[0]} vs {phrases[2]} : {util.cos_sim(embedding1,embedding3)} \n{phrases[1]} vs {phrases[2]} : {util.cos_sim(embedding2,embedding3)}")
