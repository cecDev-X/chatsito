import { Box, Stack } from "@mui/material";

interface HomeProps {
    mode: 'light' | 'dark';
    setMode: React.Dispatch<React.SetStateAction<'light' | 'dark'>>;

}
const Home: React.FC<HomeProps> = ({ mode, setMode }) => {

    return(
        <Box>
            <Stack direction="row" spacing={{xs:0, sm:2}} sx={{justifyContent:"space-between"}}>
                <h1>Welcome In Home</h1>
            </Stack>
        </Box>
    )
}
export default Home;