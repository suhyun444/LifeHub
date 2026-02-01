cd front-end
rm -rf .next 
rm -rf out
npx next build

ssh suhyun444@172.30.1.86 "rm -rf /var/www/html/card/*"

scp -r ./out/* suhyun444@172.30.1.86:/var/www/html/card/